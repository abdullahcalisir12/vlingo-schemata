package io.vlingo.schemata.codegen.processor.types;

import io.vlingo.actors.Actor;
import io.vlingo.schemata.codegen.ast.FieldDefinition;
import io.vlingo.schemata.codegen.ast.Node;
import io.vlingo.schemata.codegen.ast.types.BasicType;
import io.vlingo.schemata.codegen.ast.types.Type;
import io.vlingo.schemata.codegen.ast.types.TypeDefinition;
import io.vlingo.schemata.codegen.processor.Processor;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

public class TypeResolverProcessor extends Actor implements Processor {
    private final TypeResolver resolver;

    public TypeResolverProcessor(TypeResolver resolver) {
        this.resolver = resolver;
    }

    @Override
    public CompletableFuture<Node> process(Node node) {
        TypeDefinition type = Processor.requireBeing(node, TypeDefinition.class);

        List<Node> processedTypes = type.children.stream()
                .map(e -> (FieldDefinition) e)
                .map(this::resolveType)
                .collect(Collectors.toList());

        completableFuture().complete(new TypeDefinition(type.category, type.typeName, processedTypes));
        return completableFuture();

    }

    private FieldDefinition resolveType(FieldDefinition fieldDefinition) {
        Type typeNode = fieldDefinition.type;

        if (typeNode instanceof BasicType) {
            BasicType basicType = (BasicType) typeNode;
            return new FieldDefinition(resolver.resolve(basicType.typeName).map(definition -> (Type) definition).orElse(basicType), fieldDefinition.version, fieldDefinition.name, fieldDefinition.defaultValue);
        }

        return fieldDefinition;
    }
}