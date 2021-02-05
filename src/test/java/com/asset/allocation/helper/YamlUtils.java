package com.asset.allocation.helper;

import com.fasterxml.jackson.databind.JavaType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.function.Supplier;

public interface YamlUtils {
    Supplier<ObjectMapper> OBJECT_MAPPER = () ->
        new ObjectMapperConfiguration(new ObjectMapper(new YAMLFactory())).getObjectMapper();

    static <T> T getObjectFromYml(final File file, final Class<T> classType) {
        try {
            return OBJECT_MAPPER
                .get()
                .readValue(file, classType);
        }
        catch (IOException e) {
            throw new IllegalStateException(String.format("File: {%s}, error: {%s}", file, e));
        }
    }

    static <T> List<T> getObjectListFromYml(final File file, final Class<T> type) {
        final ObjectMapper mapper = OBJECT_MAPPER.get();
        try {
            final JavaType valueType = mapper
                .getTypeFactory()
                .constructCollectionType(List.class, type);
            return mapper.readValue(file, valueType);
        }
        catch (IOException e) {
            throw new IllegalStateException(String.format("File: {%s}, error: {%s}", file, e));
        }
    }

}
