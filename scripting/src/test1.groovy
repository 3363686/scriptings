/**
 * Created by _ame_ on 08.07.2015 12:40.
 */
Integer.metaClass.getDaysFromNow = { ->
    Calendar today = Calendar.instance
    today.add(Calendar.DAY_OF_MONTH, delegate)
    today.time
}

println(5.daysFromNow)
System.out.println( TimeZone.getDefault().getDisplayName() );

Calendar currentDateNTime = Calendar.getInstance();
Date date = currentDateNTime.getTime();
System.out.println(date);

int offset = currentDateNTime.get(Calendar.ZONE_OFFSET);
int dstoffset = currentDateNTime.get(Calendar.DST_OFFSET);
TimeZone utcTimeZone = TimeZone.getTimeZone("UTC");
System.out.println("utcTimeZone ===" +utcTimeZone);
System.out.println();
System.out.println("ZONE offset ==="+offset);
System.out.println("DST offset ====" + dstoffset);

