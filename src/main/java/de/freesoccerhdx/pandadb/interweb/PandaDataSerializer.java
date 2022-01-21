package de.freesoccerhdx.pandadb.interweb;

public interface PandaDataSerializer<T> {

    void serialize(DatabaseWriter writer);
    void deserialize(DatabaseReader reader);



}
