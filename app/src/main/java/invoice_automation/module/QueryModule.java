package invoice_automation.module;

import org.jooq.DSLContext;
import org.jooq.Record;
import org.jooq.SQLDialect;
import org.jooq.SelectConditionStep;
import org.jooq.impl.DSL;

public class QueryModule {
    private static final DSLContext dslContext = DSL.using(SQLDialect.DEFAULT);
    private static final String CUSTOMER_TABLE_NAME = "customer";

    private static final String DISPLAY_NAME_CUSTOMER_NAME = "DisplayName";


    public static String getQueryForCustomerFromSchool(String schoolName)  {
        SelectConditionStep<Record> query = dslContext.select()
                .from(CUSTOMER_TABLE_NAME)
                .where(DSL.field(DISPLAY_NAME_CUSTOMER_NAME, String.class).eq(schoolName));
        return query.toString();
    }
}
