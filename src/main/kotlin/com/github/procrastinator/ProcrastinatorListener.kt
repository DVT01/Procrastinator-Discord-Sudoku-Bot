package com.github.procrastinator

import org.javacord.api.entity.message.Message
import org.javacord.api.entity.message.MessageBuilder
import org.javacord.api.entity.message.embed.EmbedBuilder
import org.javacord.api.event.message.MessageCreateEvent
import org.javacord.api.listener.message.MessageCreateListener
import org.obebeokeke.sudoku.Sudoku
import java.awt.Color

class ProcrastinatorListener : MessageCreateListener {

    private val sudoku = Sudoku()
    private var lastMessageSent: Message? = null

    override fun onMessageCreate(event: MessageCreateEvent) {

        // All commands must start with >
        if (event.messageContent.startsWith(">")) {

            val message = event.messageContent.removePrefix(">")

            // Syntax should be '>put <row> <column> <number>'
            val putCommandRegex = Regex("^(put) [1-9] [1-9] [1-9]$")

            // Syntax should be '>empty <row> <column>'
            val emptyCommandRegex = Regex("^(empty) [1-9] [1-9]$")

            when {
                message == "board" -> sendBoard(event, sudoku.getBoard())
                message == "solve" -> sendBoard(event, sudoku.getSolvedBoard())
                message == "help" -> helpMessage(event)
                putCommandRegex.matches(message) ->
                    fillCell(
                        event,
                        message.removePrefix("put ")
                    )
                emptyCommandRegex.matches(message) ->
                    emptyCell(
                        event,
                        message.removePrefix("empty ")
                    )
            }
        }
    }

    private fun helpMessage(event: MessageCreateEvent) {
        MessageBuilder()
            .append("The prefix is '>'")
            .append("\nA 0 means that the cell is empty")
            .append("\n\nThe commands are:")
            .appendCode("text", "board")
            .append("Sends the Sudoku board")
            .appendCode("text", "put <row> <column> <number>")
            .append("Put <number> in cell at <row> and <column>")
            .appendCode("text", "empty <row> <column>")
            .append("Empties the cell at <row> and <column>" +
                    ", if it was originally empty"
            )
            .append("\n\nCommands 'put' and 'empty' update the last board sent")
            .send(event.channel)
    }

    private fun sendBoard(
        event: MessageCreateEvent,
        board: MutableList<MutableList<Int>>
    ) {
        this.lastMessageSent = event.channel.sendMessage(boardEmbed(event, board)).join()
    }

    private fun updateBoard(
        event: MessageCreateEvent,
        board: MutableList<MutableList<Int>>
    ) {
        this.lastMessageSent?.edit(boardEmbed(event, board))?.join()
    }

    private fun fillCell(
        event: MessageCreateEvent,
        message: String
    ) {
        // Message equals '<row> <column> <number>'

        val split = message.split(" ")

        val rowIndex = split[0].toInt() - 1
        val columnIndex = split[1].toInt() - 1
        val number = split[2].toInt()

        this.sudoku.fillCell(rowIndex, columnIndex, number)
        updateBoard(event, sudoku.getBoard())
    }

    private fun emptyCell(
        event: MessageCreateEvent,
        message: String
    ) {
        // Message equals '<row> <column>'

        val split = message.split(" ")

        val rowIndex = split[0].toInt() - 1
        val columnIndex = split[1].toInt() - 1

        sudoku.emptyCell(rowIndex, columnIndex)
        updateBoard(event, sudoku.getBoard())
    }

    private fun boardEmbed(
        event: MessageCreateEvent,
        board: MutableList<MutableList<Int>>
    ): EmbedBuilder {

        return EmbedBuilder()
            .setAuthor(
                "${event.messageAuthor.name}'s board",
                null,
                event.messageAuthor.avatar
            )
            .setColor(Color(140, 0, 153, 1))
            .addField(
                "FuCk YoU",
                prettifyBoard(board)
            )
    }

    private fun prettifyBoard(board: MutableList<MutableList<Int>>): String {

        // Format the board using a multiline code block to utilize its monospace font
        var prettyBoard = "```\n"

        board.forEachIndexed { rowIndex, row ->

            if (rowIndex == 0)
                prettyBoard += "\n${"-".repeat(25)}\n"

            row.forEachIndexed { numberIndex, number ->

                if (numberIndex == 0) prettyBoard += "| "

                prettyBoard += "$number "

                if ((numberIndex + 1) % 3 == 0) prettyBoard += "| "
            }

            if ((rowIndex + 1) % 3 == 0)
                prettyBoard += "\n${"-".repeat(25)}"

            prettyBoard += "\n"
        }

        prettyBoard += "\n```"

        return prettyBoard
    }
}