package com.adawatie.utils

enum class ResponseCode constructor(val code: Int) {
    OK(200),
    BadRequest(400),
    Unauthenticated(401),
    Unauthorized(403),
    NotFound(404),
    Conflict(409),
    InValidateData(422),
    Blocked(423),
    ForceUpdate(426),
    ServerError(500);
}