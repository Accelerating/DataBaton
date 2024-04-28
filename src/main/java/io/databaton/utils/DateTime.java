package io.databaton.utils;

import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
public class DateTime {

    public static final DateTimeFormatter DEFAULT_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final long timestamp;


    public DateTime(){
        this(System.currentTimeMillis());
    }

    public DateTime(long timestamp){
        this.timestamp = timestamp;
    }

    public DateTime(String dateTime, String format){
        ZonedDateTime zonedDateTime = DateTime.stringToZonedDateTime(dateTime, ZoneId.systemDefault(), format);
        this.timestamp = zonedDateTime.toInstant().toEpochMilli();
    }

    public long toTimestamp(){
        return this.timestamp;
    }

    public String toString(){
        return toLocalDateTime().format(DEFAULT_FORMATTER);
    }

    public String toString(String format){
        return toLocalDateTime().format(DateTimeFormatter.ofPattern(format));
    }

    public DateTime plusTime(int offset, TimeUnit unit){
        if(offset == 0){
            return this;
        }
        long millis = unit.toMillis(Math.abs(offset));
        if(offset < 0){
            return new DateTime(timestamp - millis);
        }
        return new DateTime(timestamp + millis);
    }

    public Date toDate(){
        return new Date(this.timestamp);
    }


    public LocalDateTime toLocalDateTime(){
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(this.timestamp), ZoneId.systemDefault());
    }

    public ZonedDateTime toZoneDateTime(){
        return ZonedDateTime.ofInstant(Instant.ofEpochMilli(this.timestamp), ZoneId.systemDefault());
    }


    public static String toDateTimeString(Date date, String format){
        return new SimpleDateFormat(format).format(date);
    }

    public static String toDateTimeString(LocalDateTime dateTime, String format){
        return dateTime.format(DateTimeFormatter.ofPattern(format));
    }

    public static long toMillTimestamp(LocalDateTime localDateTime){
        return localDateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli();
    }

    public static Date localDateTimeToDate(LocalDateTime localDateTime){
        ZonedDateTime zonedDateTime = localDateTime.atZone(ZoneId.systemDefault());
        return zonedDateTimeToDate(zonedDateTime);
    }

    public static Date zonedDateTimeToDate(ZonedDateTime zonedDateTime){
        return Date.from(zonedDateTime.toInstant());
    }

    public static LocalDateTime dateToLocalDateTime(Date date){
        Instant instant = date.toInstant();
        return instant.atZone(ZoneId.systemDefault()).toLocalDateTime();
    }

    public static ZonedDateTime dateToZoneDateTime(Date date){
        Instant instant = date.toInstant();
        return instant.atZone(ZoneId.systemDefault());
    }

    public static Date stringToDate(String dateTimeStr, String format){
        try{
            return new SimpleDateFormat(format).parse(dateTimeStr);
        }catch (Exception e){
            log.error("parse datetime string failed", e);
        }
        return null;
    }

    public static LocalDateTime stringToLocalDateTime(String dateTimeStr, String format){
        return stringToZonedDateTime(dateTimeStr, ZoneId.systemDefault(), format).toLocalDateTime();
    }

    public static ZonedDateTime stringToZonedDateTime(String dateTimeStr, ZoneId zoneId, String format){
        TemporalAccessor parse = DateTimeFormatter.ofPattern(format).withZone(zoneId).parse(dateTimeStr);
        return ZonedDateTime.from(parse);
    }

}
