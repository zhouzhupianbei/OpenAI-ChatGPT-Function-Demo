package com.lgf.warehouse.core;

/**
 * 常量服务
 */
public interface Constants {

    public interface Result {
        public interface Code {
            public static final Integer SUCCESS = 0;
            public static final Integer ERROR = 500;
        }

        public interface Type{
            public static final String TEXT = "text";
            public static final String IMAGE = "image";
            public static final String AUDIO = "audio";
            public static final String VIDEO = "video";
            public static final String FILE = "file";
        }
    }
}
