package de.freesoccerhdx.pandadb.clientutils;

public interface PandaDataSerializer<T> {

    void serialize(DatabaseWriter writer);
    void deserialize(DatabaseReader reader);



}
