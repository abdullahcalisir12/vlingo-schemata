// Copyright © 2012-2020 VLINGO LABS. All rights reserved.
//
// This Source Code Form is subject to the terms of the
// Mozilla Public License, v. 2.0. If a copy of the MPL
// was not distributed with this file, You can obtain
// one at https://mozilla.org/MPL/2.0/.

package io.vlingo.schemata.infra.persistence;

import io.vlingo.lattice.model.DomainEvent;
import io.vlingo.lattice.model.IdentifiedDomainEvent;
import io.vlingo.lattice.model.projection.Projectable;
import io.vlingo.lattice.model.projection.StateStoreProjectionActor;
import io.vlingo.schemata.model.Events.*;
import io.vlingo.schemata.model.SchemaVersion.Status;
import io.vlingo.schemata.query.view.SchemaVersionView;
import io.vlingo.symbio.Entry;
import io.vlingo.symbio.store.state.StateStore;

import java.util.ArrayList;
import java.util.List;

public class SchemaVersionProjection extends StateStoreProjectionActor<SchemaVersionView> {
    private String dataId;
    private final List<IdentifiedDomainEvent> events;

    public SchemaVersionProjection(StateStore stateStore) {
        super(stateStore);

        this.events = new ArrayList<>(2);
    }

    @Override
    protected SchemaVersionView currentDataFor(Projectable projectable) {
        return SchemaVersionView.with(projectable.dataId());
    }

    @Override
    protected String dataIdFor(Projectable projectable) {
        final IdentifiedDomainEvent event = events.get(0);
        dataId = dataIdFrom(":", event.parentIdentity(), event.identity());
        return dataId;
    }

    @Override
    protected SchemaVersionView merge(SchemaVersionView previousData, int previousVersion, SchemaVersionView currentData, int currentVersion) {
        return previousData == null
                ? mergeEventsInto(currentData)
                : mergeEventsInto(previousData);
    }

    @Override
    protected void prepareForMergeWith(Projectable projectable) {
        events.clear();

        for (final Entry<?> entry : projectable.entries()) {
            events.add(entryAdapter().anyTypeFromEntry(entry));
        }
    }

    private SchemaVersionView mergeEventsInto(final SchemaVersionView initialData) {
        SchemaVersionView mergedData = initialData;
        for (DomainEvent event : events) {
            switch (SchemaVersionViewType.match(event)) {
                case SchemaVersionDefined:
                    final SchemaVersionDefined defined = typed(event);
                    mergedData = SchemaVersionView.with(defined.organizationId, defined.unitId, defined.contextId, defined.schemaId,
                            defined.schemaVersionId, defined.description, defined.specification, defined.status, defined.previousVersion, defined.nextVersion);
                    break;
                case SchemaVersionDescribed:
                    final SchemaVersionDescribed described = typed(event);
                    mergedData = mergedData.mergeDescriptionWith(described.schemaVersionId, described.description);
                    break;
                case SchemaVersionAssigned:
                    final SchemaVersionAssigned assigned = typed(event);
                    mergedData = mergedData.mergeVersionWith(assigned.schemaVersionId, assigned.version);
                    break;
                case SchemaVersionSpecified:
                    final SchemaVersionSpecified specified = typed(event);
                    mergedData = mergedData.mergeSpecificationWith(specified.schemaVersionId, specified.specification);
                    break;
                case SchemaVersionPublished:
                    final SchemaVersionPublished published = typed(event);
                    mergedData = mergedData.mergeStatusWith(published.schemaVersionId, Status.Published.name());
                    break;
                case SchemaVersionDeprecated:
                    final SchemaVersionDeprecated deprecated = typed(event);
                    mergedData = mergedData.mergeStatusWith(deprecated.schemaVersionId, Status.Deprecated.name());
                    break;
                case SchemaVersionRemoved:
                    final SchemaVersionRemoved removed = typed(event);
                    mergedData = mergedData.mergeStatusWith(removed.schemaVersionId, Status.Removed.name());
                    break;
                case Unmatched:
                    logger().warn("Event of type " + event.typeName() + " was not matched.");
                    break;
            }
        }

        logger().info("PROJECTED: " + mergedData);

        return mergedData;
    }
}
