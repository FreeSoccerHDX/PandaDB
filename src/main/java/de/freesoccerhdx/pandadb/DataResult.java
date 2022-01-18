package de.freesoccerhdx.pandadb;

import java.util.ArrayList;
import java.util.List;

public class DataResult {

    interface Result {
    }

    private interface TypeListResult<E> extends Result {
        void resultList(ArrayList<E> list, Status status);
    }


    public interface StatusResult extends Result {
        void result(Status status);

    }

    public interface ValueResult extends Result {
        void result(Double data, Status status);
    }

    public interface ValuesInfoResult extends Result {
        void result(ValueDataStorage.ValueMembersInfo data, Status status);
    }

    public interface TextResult extends Result {
        void result(String data, Status status);
    }


    public interface ListResult<E> extends TypeListResult<E> {
        void resultList(ArrayList<E> data, Status status);
    }

    public interface KeysResult<String> extends TypeListResult<String> {
        void resultList(ArrayList<String> data, Status status);
    }




}
