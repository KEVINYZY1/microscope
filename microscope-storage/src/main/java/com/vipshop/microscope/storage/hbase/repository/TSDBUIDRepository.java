package com.vipshop.microscope.storage.hbase.repository;

import com.vipshop.microscope.storage.hbase.table.TSDBUIDTable;
import org.springframework.stereotype.Repository;

@Repository
public class TSDBUIDRepository extends AbstraceRepository {

    public void initialize() {
        super.initialize(TSDBUIDTable.TABLE_NAME, new String[]{TSDBUIDTable.CF_NAME, TSDBUIDTable.CF_ID});
    }

    public void drop() {
        super.drop(TSDBUIDTable.TABLE_NAME);
    }

}
