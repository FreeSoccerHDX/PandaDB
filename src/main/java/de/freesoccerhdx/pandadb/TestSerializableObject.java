package de.freesoccerhdx.pandadb;

import de.freesoccerhdx.pandadb.clientutils.DatabaseReader;
import de.freesoccerhdx.pandadb.clientutils.DatabaseWriter;
import de.freesoccerhdx.pandadb.clientutils.PandaDataSerializer;

import java.io.Writer;

public class TestSerializableObject implements PandaDataSerializer {


    public TestSerializableObject(){

    }


    @Override
    public void serialize(DatabaseWriter writer) {
        writer.writeString("key", "text");
    }

    @Override
    public void deserialize(DatabaseReader reader) {
        assert reader.getString("key") != null;
    }
}
