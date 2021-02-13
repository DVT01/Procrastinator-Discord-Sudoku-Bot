package org.obebeokeke.sudoku

import retrofit2.Call
import retrofit2.http.GET

interface SudokuApi {

    @GET("board?difficulty=easy")
    fun fetchBoard(): Call<SudokuBoard>
}