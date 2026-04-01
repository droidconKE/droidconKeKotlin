package com.android254.presentation.common.results_status

sealed interface ResultStatus{
    object Loading: ResultStatus
    object Success: ResultStatus
    data class Empty(val message: String): ResultStatus
    data class Error(val message: String): ResultStatus
}

val ResultStatus.isLoading: Boolean
    get() = this is ResultStatus.Loading

val ResultStatus.isEmpty: Boolean
    get() = this is ResultStatus.Empty

val ResultStatus.isSuccess: Boolean
    get() = this is ResultStatus.Success

val ResultStatus.isError: Boolean
    get() = this is ResultStatus.Error

val ResultStatus.emptyMessage: String
    get() = (this as? ResultStatus.Empty)?.message ?: ""

val ResultStatus.errorMessage: String
    get() = (this as? ResultStatus.Error)?.message ?: ""