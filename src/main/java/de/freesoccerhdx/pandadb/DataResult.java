package de.freesoccerhdx.pandadb;

import java.util.ArrayList;
import java.util.List;

public class DataResult {

    interface Result<T> {
    }

    private interface TypeListResult<E> extends Result<List> {
        void resultList(ArrayList<E> list, Status status);
    }


    public interface StatusResult<Status> extends Result<Status> {
        void result(Status status);

    }

    public interface ValueResult<Double> extends Result<Double> {
        void result(Double data, Status status);
    }

    public interface TextResult<String> extends Result<String> {
        void result(String data, Status status);
    }


    public interface ListResult<E> extends TypeListResult<E> {
        void resultList(ArrayList<E> data, Status status);
    }

    public interface KeysResult<String> extends TypeListResult<String> {
        void resultList(ArrayList<String> data, Status status);
    }




}
