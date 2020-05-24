package com.llm.membershipadmin;

public enum MemberShipResultMessage {
    Ok,
    Error,
    NotFound,
    Suspended,
    Conflict //Om vi hittar samma post blir det Conflict
}
