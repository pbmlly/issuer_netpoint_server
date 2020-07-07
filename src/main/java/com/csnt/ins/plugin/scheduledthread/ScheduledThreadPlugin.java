package com.csnt.ins.plugin.scheduledthread;

import com.jfinal.kit.Prop;
import com.jfinal.kit.PropKit;
import com.jfinal.kit.StrKit;
import com.jfinal.log.Log;
import com.jfinal.plugin.IPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;


/**
 * @author source
 */
@SuppressWarnings("Duplicates")
public class ScheduledThreadPlugin implements IPlugin {
    private static Log LOG = Log.getLog("scheduler");


    /**
     * 调度线程池
     */
    private int scheduledThreadPoolSize = 10;

    /**
     * ScheduledThreadPoolExecutor调度器
     */
    private ScheduledThreadPoolExecutor fixedScheduler;


    /**
     * 调度任务配置文件
     */
    private final String jobConfigFile;


    /**
     * 是否有ScheduledThreadPoolExecutor任务
     */
    private boolean hasFixedJob = false;

    /**
     * <p>Title: SchedulerPlugin</p>
     * <p>Description: 构造函数(指定调度线程池大小、调度任务配置文件和扫描路径)</p>
     *
     * @param scheduledThreadPoolSize 调度线程池大小
     * @param jobConfigFile           调度任务配置文件
     * @since V1.0.0
     */
    private ScheduledThreadPlugin(int scheduledThreadPoolSize, String jobConfigFile) {
        this.scheduledThreadPoolSize = scheduledThreadPoolSize;
        this.jobConfigFile = jobConfigFile;
    }


    /**
     * @Title: ensurFixedScheduler
     * @Description: 确保fixedScheduler可用
     * @since V1.0.0
     */
    private void ensurFixedScheduler() {
        if (this.fixedScheduler == null) {
            synchronized (this) {
                if (this.fixedScheduler == null) {
                    this.fixedScheduler = new ScheduledThreadPoolExecutor(scheduledThreadPoolSize);
                }
            }
        }
    }

    /**
     * @param job                 定期执行的任务
     * @param initialDelaySeconds 启动延迟时间
     * @param periodSeconds       每次执行任务的间隔时间(单位秒)
     * @return
     * @Title: scheduleAtFixedRate
     * @Description: 延迟指定秒后启动，并以固定的频率来运行任务。后续任务的启动时间不受前次任务延时影响（并行）。
     * @since V1.0.0
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable job, int initialDelaySeconds, int periodSeconds) {
        ensurFixedScheduler();
        this.hasFixedJob = true;
        return fixedScheduler.scheduleAtFixedRate(job, initialDelaySeconds, periodSeconds, TimeUnit.SECONDS);
    }

    /**
     * @param job                 定期执行的任务
     * @param initialDelaySeconds 启动延迟时间
     * @param periodSeconds       每次执行任务的间隔时间(单位秒)
     * @return
     * @Title: scheduleWithFixedDelay
     * @Description: 延迟指定秒后启动，两次任务间保持固定的时间间隔(任务串行执行，前一个结束之后间隔固定时间后一个才会启动)
     * @since V1.0.0
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable job, int initialDelaySeconds, int periodSeconds) {
        ensurFixedScheduler();
        this.hasFixedJob = true;
        return fixedScheduler.scheduleWithFixedDelay(job, initialDelaySeconds, periodSeconds, TimeUnit.SECONDS);
    }

    @Override
    public boolean start() {
        //通过文件记载
        loadJobsFromConfigFile();
        //启动fixed任务
        if (this.hasFixedJob) {
            LOG.info("ScheduledThreadPoolExecutor已启动");
        }
        return true;
    }

    @Override
    public boolean stop() {
        //停止fixed任务
        if (this.hasFixedJob) {
//            //暴力强制停止
//            this.fixedScheduler.shutdownNow();
//            this.fixedScheduler = null;
            //优雅的停止
            this.fixedScheduler.shutdown();
            LOG.info("ScheduledThreadPoolExecutor开始停止");
            try {
                while (!fixedScheduler.awaitTermination(1, TimeUnit.SECONDS)) {
                    LOG.info("SchedulerPlugin正在停止");
                }
            } catch (Throwable t) {
                LOG.error("SchedulerPlugin停止失败");
                return false;
            }
        }
        LOG.info("SchedulerPlugin已停止");
        return true;
    }

    /**
     * @Title: loadJobsFromConfigFile
     * @Description: 从配置文件汇总加载任务
     * @since V1.0.0
     */
    private void loadJobsFromConfigFile() {
        if (StrKit.isBlank(this.jobConfigFile)) {
            return;
        }
        // 获取job配置文件
        Prop jobProp = PropKit.use(this.jobConfigFile);
        // 获得所有任务名
        Set<String> jobNames = this.getJobNamesFromProp(jobProp);
        if (jobNames.isEmpty()) {
            return;
        }
        // 逐个加载任务
        for (String jobName : jobNames) {
            loadJob(jobProp, jobName);
        }
    }

    /**
     * @param jobProp job配置
     * @param jobName job名
     * @Title: loadJob
     * @Description: 加载一个任务
     * @since V1.0.0
     */
    private void loadJob(Prop jobProp, String jobName) {
        // 任务开关，默认开启
        Boolean enable = jobProp.getBoolean(jobName + ".enable", Boolean.FALSE);
        // 任务被禁用，直接返回
        if (!enable) {
            return;
        }

        // 创建要执行的任务
        Runnable runnable = createRunnableJob(jobName, jobProp.get(jobName + ".class"));
        int fixedRate = jobProp.getInt(jobName + ".fixedRate", 0);
        int fixedDelay = jobProp.getInt(jobName + ".fixedDelay", 0);
        int initialDelay = jobProp.getInt(jobName + ".initialDelay", 1);
        //参数检查
        int doubleCheckCounter = 0;
        if (fixedDelay != 0) {
            doubleCheckCounter++;
        }
        if (fixedRate != 0) {
            doubleCheckCounter++;
        }
        if (doubleCheckCounter != 1) {
            throw new RuntimeException(jobName + "的fixedDelay/fixedRate需要且只能设定其中一个");
        }
        if (fixedRate != 0) {
            this.scheduleAtFixedRate(runnable, initialDelay, fixedRate);
            LOG.info("通过配置文件自动加载FixedRate类型定时任务( jobName=" + jobName + ", initialDelay=" + initialDelay + "'s, fixedRate=" + fixedRate + "'s )");
        } else {
            this.scheduleWithFixedDelay(runnable, initialDelay, fixedDelay);
            LOG.info("通过配置文件自动加载FixedDelay类型定时任务( jobName=" + jobName + ", initialDelay=" + initialDelay + "'s, fixedDelay=" + fixedDelay + "'s )");
        }
    }

    /**
     * @param jobName      任务名
     * @param jobClassName 任务类名
     * @return Runnable对象
     * @Title: createRunnableJob
     * @Description: 创建任务
     * @since V1.0.0
     */
    private Runnable createRunnableJob(String jobName, String jobClassName) {
        if (jobClassName == null) {
            throw new RuntimeException("请设定 " + jobName + ".class");
        }

        Object temp = null;
        try {
            temp = Class.forName(jobClassName).newInstance();
        } catch (Exception e) {
            throw new RuntimeException("无法实例化类: " + jobClassName, e);
        }

        Runnable job = null;
        if (temp instanceof Runnable) {
            job = (Runnable) temp;
        } else {
            throw new RuntimeException("无法实例化类: " + jobClassName
                    + "，该类必须实现Runnable接口");
        }
        return job;
    }

    /**
     * @param jobProp job配置
     * @return 任务名集合
     * @Title: getJobNamesFromProp
     * @Description: 获得所有任务名
     * @since V1.0.0
     */
    private Set<String> getJobNamesFromProp(Prop jobProp) {
        Map<String, Boolean> jobNames = new HashMap<String, Boolean>(16);
        for (Object item : jobProp.getProperties().keySet()) {
            String fullKeyName = item.toString();
            // 获得job名
            String jobName = fullKeyName.substring(0, fullKeyName.indexOf("."));
            jobNames.put(jobName, Boolean.TRUE);
        }
        return jobNames.keySet();
    }


    /**
     * @return
     * @Title: builder
     * @Description: 返回一个构建器
     * @since V1.0.0
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        /**
         * 调度线程池大小
         */
        private int scheduledThreadPoolSize;

        /**
         * 调度任务配置文件
         */
        private String jobConfigFile = null;


        /**
         * <p>Title: Builder</p>
         * <p>Description: 默认构造函数</p>
         *
         * @since V1.0.0
         */
        public Builder() {
            this.scheduledThreadPoolSize = this.getBestPoolSize();
        }

        /**
         * @param size
         * @return
         * @Title: scheduledThreadPoolSize
         * @Description: 配置调度线程池大小
         * @since V1.0.0
         */
        public Builder scheduledThreadPoolSize(int size) {
            this.scheduledThreadPoolSize = size;
            return this;
        }

        /**
         * @param configFile
         * @return
         * @Title: enableConfigFile
         * @Description: 使能配置文件加载
         * @since V1.0.0
         */
        public Builder enableConfigFile(String configFile) {
            this.jobConfigFile = configFile;
            return this;
        }


        /**
         * @return
         * @Title: getBestPoolSize
         * @Description: 获得调度线程池大小
         * @since V1.0.0
         */
        private int getBestPoolSize() {
            try {
                final int cores = Runtime.getRuntime().availableProcessors();
                // 每个核有8个调度线程
                return cores * 8;
            } catch (Throwable e) {
                return 8;
            }
        }

        /**
         * @return
         * @Title: build
         * @Description: 构建一个调度器插件
         * @since V1.0.0
         */
        public ScheduledThreadPlugin build() {
            return new ScheduledThreadPlugin(this.scheduledThreadPoolSize, this.jobConfigFile);
        }
    }
}
