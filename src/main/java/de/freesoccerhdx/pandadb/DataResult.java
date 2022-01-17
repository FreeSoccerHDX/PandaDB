package de.freesoccerhdx.pandadb;

import java.util.ArrayList;

public class DataResult {

    interface Result<T> {
        void result(T data, boolean successful);
    }

    public interface SetResult<Boolean> extends Result<Boolean>{
        void result(boolean successful);

        default void result(Boolean data, boolean successful) {
        }
    }

    public interface RemoveResult<Boolean> extends Result<Boolean>{
        void result(boolean successful);

        default void result(Boolean data, boolean successful) {

        }
    }

    public interface AddListResult<Boolean> extends Result<Boolean>{
        void result(boolean successful);

        default void result(Boolean data, boolean successful) {

        }
    }

    public interface ValueResult<Double> extends Result<Double> {
        @Override
        void result(Double data, boolean successful);
    }

    public interface TextResult<String> extends Result<String> {
        @Override
        void result(String data, boolean successful);
    }


    public interface ListResult<List> extends Result<List> {
        void result(ArrayList<Object> data, boolean successful);

        @Override
        default void result(List data, boolean successful){
            ArrayList<Object> objects = (ArrayList<Object>) data;
            this.result(objects,successful);
        }
    }




}
