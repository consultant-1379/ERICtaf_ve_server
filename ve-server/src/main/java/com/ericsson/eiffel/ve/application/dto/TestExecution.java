package com.ericsson.eiffel.ve.application.dto;

import com.google.common.base.Charsets;
import com.google.common.hash.Hashing;

import static com.google.common.base.Strings.nullToEmpty;

public class TestExecution {

    private final String id;
    private final String job;
    private final String suite;
    private final String test;
    private final String execution;
    private final String result;

    public TestExecution(String job,
                         String suite,
                         String test,
                         String execution,
                         String result) {
        this.job = job;
        this.suite = suite;
        this.test = test;
        this.execution = execution;
        this.result = result;

        this.id = Hashing.sha1().newHasher()
                .putString(nullToEmpty(job), Charsets.UTF_8)
                .putString(nullToEmpty(suite), Charsets.UTF_8)
                .putString(nullToEmpty(test), Charsets.UTF_8)
                .putString(nullToEmpty(execution), Charsets.UTF_8)
                .hash().toString();
    }

    public String getId() {
        return id;
    }

    public String getJob() {
        return job;
    }

    public String getSuite() {
        return suite;
    }

    public String getTest() {
        return test;
    }

    public String getExecution() {
        return execution;
    }

    public String getResult() {
        return result;
    }

}
