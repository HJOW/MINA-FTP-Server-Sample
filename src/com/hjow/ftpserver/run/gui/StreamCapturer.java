/*
This project is just simple implementation of Apache MINA FTP server.

Copyright 2023 HJOW

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.

This code is from https://stackoverflow.com/questions/26270382/how-to-take-terminal-results-and-set-a-jtextarea-to-read-the-terminal/26270641#26270641
*/
package com.hjow.ftpserver.run.gui;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

public class StreamCapturer extends OutputStream {
    private StreamConsumer consumer;
    private PrintStream old;

    public StreamCapturer(StreamConsumer consumer, PrintStream old) {
        this.old = old;
        this.consumer = consumer;
    }

    @Override
    public void write(int b) throws IOException {
        char c = (char) b;
        consumer.appendText(Character.toString(c));
        old.print(c);
    }
}