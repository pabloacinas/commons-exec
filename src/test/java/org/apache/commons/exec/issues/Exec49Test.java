/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.commons.exec.issues;

import java.io.ByteArrayOutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;
import java.time.Duration;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecuteResultHandler;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.Executor;
import org.apache.commons.exec.PumpStreamHandler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;

/**
 * Test EXEC-44 (https://issues.apache.org/jira/browse/EXEC-44).
 */
public class Exec49Test {

    private static final Duration WAIT = Duration.ofSeconds(10);
    private final Executor exec = DefaultExecutor.builder().get();

    /**
     * The issue was detected when trying to capture stdout/stderr with a PipedOutputStream and then pass that to a PipedInputStream. The following code will
     * produce the error. The reason for the error is the PipedOutputStream is not being closed correctly, causing the PipedInputStream to break.
     *
     * @throws Exception the test failed
     */
    /*
    @Test
    @DisabledOnOs(org.junit.jupiter.api.condition.OS.WINDOWS)
    public void testExec49_1() throws Exception {
        final CommandLine cl = CommandLine.parse("/bin/ls");
        cl.addArgument("/opt");
        // redirect stdout/stderr to pipedOutputStream
        try (PipedOutputStream pipedOutputStream = new PipedOutputStream()) {
            final PumpStreamHandler psh = new PumpStreamHandler(pipedOutputStream);
            exec.setStreamHandler(psh);
            // start an asynchronous process to enable the main thread
            System.out.println("Preparing to execute process - commandLine=" + cl.toString());
            final DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
            exec.execute(cl, handler);
            System.out.println("Process spun off successfully - process=" + cl.getExecutable());
            try (PipedInputStream pis = new PipedInputStream(pipedOutputStream)) {
                while (pis.read() >= 0) {
//                 System.out.println("pis.available() " + pis.available());
//                 System.out.println("x " + x);
                }
            }
            handler.waitFor(WAIT);
            handler.getExitValue(); // will fail if process has not finished
        }
    }
    */
    @Test
    @DisabledOnOs(org.junit.jupiter.api.condition.OS.WINDOWS)
    public void testExec49_1() throws Exception {
        final CommandLine cl = CommandLine.parse("/bin/ls");
        cl.addArgument("/opt");
        
        // redirect stdout/stderr to pipedOutputStream
        try (PipedOutputStream pipedOutputStream = new PipedOutputStream()) {
            final PumpStreamHandler psh = new PumpStreamHandler(pipedOutputStream);
            exec.setStreamHandler(psh);  // Asumiendo que 'exec' está inicializado adecuadamente
            
            // start an asynchronous process to enable the main thread
            System.out.println("Preparing to execute process - commandLine=" + cl.toString());
            final DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
            exec.execute(cl, handler);
            System.out.println("Process spun off successfully - process=" + cl.getExecutable());
            
            // Read from the piped output stream
            try (PipedInputStream pis = new PipedInputStream(pipedOutputStream)) {
                int byteRead;
                StringBuilder output = new StringBuilder();
                while ((byteRead = pis.read()) >= 0) {
                    output.append((char) byteRead);
                }

                // Assert that the output contains the expected result
                assertTrue(output.toString().contains(EXPECTED_OUTPUT), "Output does not contain expected result");
            }

            // Wait for the process to finish and assert that the exit value is 0 (success)
            handler.waitFor(WAIT);
            int exitValue = handler.getExitValue();
            assertEquals(0, exitValue, "Process did not complete successfully. Exit value: " + exitValue);
        }
    }

    /**
     * The issue was detected when trying to capture stdout with a PipedOutputStream and then pass that to a PipedInputStream. The following code will produce
     * the error. The reason for the error is the PipedOutputStream is not being closed correctly, causing the PipedInputStream to break.
     *
     * @throws Exception the test failed
     */
    /*
 
    @Test
    @DisabledOnOs(org.junit.jupiter.api.condition.OS.WINDOWS)
    public void testExec49_2() throws Exception {
        final CommandLine cl = CommandLine.parse("/bin/ls");
        cl.addArgument("/opt");
        // redirect only stdout to pipedOutputStream
        try (PipedOutputStream pipedOutputStream = new PipedOutputStream()) {
            final PumpStreamHandler psh = new PumpStreamHandler(pipedOutputStream, new ByteArrayOutputStream());
            exec.setStreamHandler(psh);
            // start an asynchronous process to enable the main thread
            System.out.println("Preparing to execute process - commandLine=" + cl.toString());
            final DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
            exec.execute(cl, handler);
            System.out.println("Process spun off successfully - process=" + cl.getExecutable());
            try (PipedInputStream pis = new PipedInputStream(pipedOutputStream)) {
                while (pis.read() >= 0) {
//                 System.out.println("pis.available() " + pis.available());
//                 System.out.println("x " + x);
                }
            }
            handler.waitFor(WAIT);
            handler.getExitValue(); // will fail if process has not finished
        }
    }
    */
    
    @Test
    @DisabledOnOs(org.junit.jupiter.api.condition.OS.WINDOWS)
    public void testExec49_2() throws Exception {
        final CommandLine cl = CommandLine.parse("/bin/ls");
        cl.addArgument("/opt");

        // Redirect only stdout to pipedOutputStream
        try (PipedOutputStream pipedOutputStream = new PipedOutputStream()) {
            final PumpStreamHandler psh = new PumpStreamHandler(pipedOutputStream, new ByteArrayOutputStream());
            exec.setStreamHandler(psh);

            // Start an asynchronous process
            System.out.println("Preparing to execute process - commandLine=" + cl.toString());
            final DefaultExecuteResultHandler handler = new DefaultExecuteResultHandler();
            exec.execute(cl, handler);

            System.out.println("Process spun off successfully - process=" + cl.getExecutable());

            try (PipedInputStream pis = new PipedInputStream(pipedOutputStream)) {
                int bytesRead = 0;
                while (pis.read() >= 0) {
                    bytesRead++;
                }

                // Add assertion to ensure that the process produces output
                assertTrue(bytesRead > 0, "The process should produce output on stdout.");
            }

            // Wait for the process to complete and validate its exit value
            handler.waitFor(WAIT);
            assertEquals(0, handler.getExitValue(), "The process should exit with code 0.");
        }
    }


}
