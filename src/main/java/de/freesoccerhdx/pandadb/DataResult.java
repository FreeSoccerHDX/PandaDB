package de.freesoccerhdx.pandadb;

import de.freesoccerhdx.pandadb.serverlisteners.MemberValueDataStorage;
import de.freesoccerhdx.simplesocket.Pair;

import java.util.ArrayList;
import java.util.HashMap;

public class DataResult {

    interface Result {
    }
    private interface TypeListResult<E> extends Result {
        void resultList(ArrayList<E> list, Status status);
    }

    public interface MemberDataResult extends Result{
        void resultData(HashMap<String, String> memberData, Status status);
    }

    public interface SpecificResult<T> extends Result {
        void result(T object, Status status);
    }

    public interface ListStoredSerializableResult<T> extends Result {
        void result(HashMap<String, T> object, Status status);
    }

    public interface SortedValueMemberDataResult extends Result {
        void result(Pair<String, Double>[] sorted, Status status);
    }

    public interface ValueMemberDataResult extends Result {
        void result(HashMap<String, Double> object, Status status);
    }

    public interface StatusResult extends Result {
        void result(Status status);
    }

    public interface ValueResult extends Result {
        void result(Double data, Status status);
    }

    public interface ListSizeResult extends Result {
        void result(Integer data, Status status);
    }

    public interface ValuesInfoResult extends Result {
        void result(MemberValueDataStorage.ValueMembersInfo data, Status status);
    }

    public interface TextResult extends Result {
        void result(String data, Status status);
    }

    public interface ListTypeValueResult<T> extends Result {
        void result(T data, Status status);
    }


    public interface ListTypeResult extends ListResult<ListType> {
        void resultList(ArrayList<ListType> list, Status status);
    }

    public interface ListResult<E> extends TypeListResult<E> {
        void resultList(ArrayList<E> data, Status status);
    }

    public interface KeysResult extends TypeListResult<String> {
        void resultList(ArrayList<String> data, Status status);
    }




}
