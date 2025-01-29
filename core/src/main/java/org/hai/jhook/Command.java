package org.hai.jhook;

import java.io.IOException;

public interface Command {
    String getCmdType();
    String execute() throws IOException;
}
