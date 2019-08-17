// Copyright © 2012-2018 Vaughn Vernon. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.schemata.infra.persistence.mappers;

import io.vlingo.schemata.model.Id.OrganizationId;
import io.vlingo.schemata.model.OrganizationState;
import org.jdbi.v3.core.mapper.RowMapper;
import org.jdbi.v3.core.statement.StatementContext;

import java.sql.ResultSet;
import java.sql.SQLException;

public class OrganizationStateMapper  implements RowMapper<OrganizationState> {
    @Override
    public OrganizationState map(ResultSet rs, StatementContext ctx) throws SQLException {
        OrganizationState organizationState = new OrganizationState(OrganizationId.existing(Long.toString(rs.getLong("id"))));
        return organizationState.define(rs.getString("name"), rs.getString("description"));
    }
}
