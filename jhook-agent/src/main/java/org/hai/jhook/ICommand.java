package org.hai.jhook;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

public interface ICommand {
    void execute(ObjectInputStream input, ObjectOutputStream output);
}
