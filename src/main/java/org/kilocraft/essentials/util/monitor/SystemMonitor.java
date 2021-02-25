package org.kilocraft.essentials.util.monitor;


import javax.management.Attribute;
import javax.management.AttributeList;
import javax.management.MBeanServer;
import javax.management.ObjectName;
import java.io.File;
import java.lang.management.ManagementFactory;
import java.text.DecimalFormat;

public class SystemMonitor {

    private static final Runtime runTime = Runtime.getRuntime();
    private static final DecimalFormat decimalFormat = new DecimalFormat("##.##");

    private static long RAM_MAX_MB, RAM_TOTAL_MB, RAM_FREE_MB, RAM_USED_MB;
    private static long DISK_MAX_GB, DISK_FREE_GB, DISK_USABLE_GB, DISK_USED_GB;

    private static double RAM_USED_PERCENTAGE, DISK_USED_PERCENTAGE;

    private static void updateDiskInfo() {
        File file = new File("/");
        long DISK_MAX = file.getTotalSpace();
        long DISK_USABLE = file.getUsableSpace();
        long DISK_FREE = file.getFreeSpace();
        DISK_MAX_GB = DISK_MAX / 1024 / 1024 / 1024;
        DISK_USABLE_GB = DISK_USABLE / 1024 / 1024 / 1024;
        DISK_FREE_GB = DISK_FREE / 1024 / 1024 / 1024;
        long DISK_USED = DISK_MAX - DISK_FREE;
        DISK_USED_GB = DISK_USED / 1024 / 1024 / 1024;
        DISK_USED_PERCENTAGE = ((double) DISK_USED / DISK_MAX) * 100;
    }

    private static void updateRamInfo() {
        long RAM_MAX = runTime.maxMemory();
        long RAM_TOTAL = runTime.totalMemory();
        long RAM_FREE = runTime.freeMemory();
        RAM_MAX_MB = RAM_MAX / 1024 / 1024;
        RAM_TOTAL_MB = RAM_TOTAL / 1024 / 1024;
        RAM_FREE_MB = RAM_FREE / 1024 / 1024;
        long RAM_USED = RAM_TOTAL - RAM_FREE;
        RAM_USED_MB = (long) ((double) RAM_USED / 1024 / 1024);
        RAM_USED_PERCENTAGE = ((double) RAM_USED / RAM_TOTAL) * 100;
    }

    public static double getCpuLoadPercentage() throws Exception {
        MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
        ObjectName name = ObjectName.getInstance("java.lang:type=OperatingSystem");
        AttributeList list = mbs.getAttributes(name, new String[]{"ProcessCpuLoad"});

        if (list.isEmpty()) {
            return Double.NaN;
        }

        Attribute att = (Attribute) list.get(0);
        Double value = (Double) att.getValue();

        // usually takes a couple of seconds before we get real values
        if (value == -1.0) {
            return Double.NaN;
        }
        // returns a percentage value with 1 decimal point precision
        return ((int) (value * 1000) / 10.0);
    }

    public static double getRamTotalMB() {
        updateRamInfo();
        return Double.parseDouble(decimalFormat.format(RAM_TOTAL_MB));
    }

    public static double getRamUsedPercentage() {
        updateRamInfo();
        return Double.parseDouble(decimalFormat.format(RAM_USED_PERCENTAGE));
    }

    public static double getRamFreeMB() {
        updateRamInfo();
        return Double.parseDouble(decimalFormat.format(RAM_FREE_MB));
    }

    public static double getRamUsedMB() {
        updateRamInfo();
        return Double.parseDouble(decimalFormat.format(RAM_USED_MB));
    }

    public static double getRamMaxMB() {
        updateRamInfo();
        return Double.parseDouble(decimalFormat.format(RAM_MAX_MB));
    }

    public static double getDiskUsedPercentage() {
        updateDiskInfo();
        return Double.parseDouble(decimalFormat.format(DISK_USED_PERCENTAGE));
    }

    public static double getDiskMaxGB() {
        updateDiskInfo();
        return DISK_MAX_GB;
    }

    public static double getDiskUsableGB() {
        updateDiskInfo();
        return DISK_USABLE_GB;
    }

    public static double getDiskFreeGB() {
        updateDiskInfo();
        return DISK_FREE_GB;
    }

    public static double getDiskUsedGB() {
        updateDiskInfo();
        return DISK_USED_GB;
    }

}
