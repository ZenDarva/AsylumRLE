package xyz.theasylum.zendarva.rle.palette.serialization;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.module.SimpleModule;
import xyz.theasylum.zendarva.rle.palette.Palette;

import java.io.IOException;

public class PaletteDeserializer {

    static class cdeserializer extends StdDeserializer<Palette> {

        protected cdeserializer(Class<?> vc) {
            super(vc);
        }

        @Override
        public Palette deserialize(JsonParser p, DeserializationContext ctxt) throws IOException, JsonProcessingException {
            String name = p.getCurrentName();
            try {
                Class clazz = Class.forName(name);
                return (Palette) ctxt.readValue(p, clazz);
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    public static ObjectMapper getDeserializer() {
        ObjectMapper mapper = new ObjectMapper();
        SimpleModule sm = new SimpleModule();
        sm.addDeserializer(Palette.class, new cdeserializer(null));
        mapper.registerModule(sm);
        return mapper;
    }
}



