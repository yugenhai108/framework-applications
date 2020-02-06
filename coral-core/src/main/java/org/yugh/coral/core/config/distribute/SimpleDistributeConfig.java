package org.yugh.coral.core.config.distribute;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;

/**
 * @author yugenhai
 */
@Data
public class SimpleDistributeConfig {

    /**
     * 工作 ID (0~31)
     */
    @Value("${distribute.sequences.work.work-id:0}")
    private int workId;

    /**
     * 数据中心 ID (0~31)
     */
    @Value("${distribute.sequences.datacenter.datacenter-id:0}")
    private int dataCenterId;
}
