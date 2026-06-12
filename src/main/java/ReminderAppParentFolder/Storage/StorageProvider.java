package ReminderAppParentFolder.Storage;

import ReminderAppParentFolder.Session.SessionDraft;

import java.io.IOException;
import java.util.List;

public interface StorageProvider<T> {

    int save(T obj);

    T load(String name);

    List<String> loadAllNames();

    int delete(String name);
}