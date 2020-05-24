package com.llm.membershipadmin;

public enum MemberShipResultMessage {
    Ok,
    AdminOk,
    Error,
    NotFound,
    Suspended,
    Conflict //Om vi hittar samma post blir det Conflict
}
