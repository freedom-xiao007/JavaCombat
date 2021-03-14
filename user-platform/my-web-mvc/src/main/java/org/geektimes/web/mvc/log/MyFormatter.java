/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.geektimes.web.mvc.log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Formatter;
import java.util.logging.LogRecord;

/**
 * @author lw1243925457
 */
public class MyFormatter extends Formatter {

    @Override
    public String format(LogRecord record) {
        // TODO Auto-generated method stub
        //创建StringBuilder对象来存放后续需要打印的日志内容
        StringBuilder builder = new StringBuilder();

        //获取时间
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss.SSS");
        Date now = new Date();
        String dateStr = sdf.format(now);

        builder.append(dateStr);
        builder.append(" - ");

        //拼接日志级别
        builder.append(record.getLevel()).append(" - ");

        //拼接方法名
        builder.append(record.getSourceMethodName()).append(" - ");

        //拼接日志内容
        builder.append(record.getMessage());
        //日志换行
        builder.append("\r\n");

        return builder.toString();
    }
}
