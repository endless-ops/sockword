package cn.dreamchase.android.greendao;


import de.greenrobot.daogenerator.DaoGenerator;
import de.greenrobot.daogenerator.Entity;
import de.greenrobot.daogenerator.Schema;

public class MyClass {

    public static void main(String[] args) {
        // 创建Schema对象，构造方法第一个参数为数据库版本号
        // 第二个参数为自动生成的实体类将要存放的位置
        Schema schema = new Schema(1000,"cn.dreamchase.android.greendao.entity");
        addNote(schema);
        try {
            // 创建实体类，第二个参数天Android Module的路径
            new DaoGenerator().generateAll(schema,"./app/src/main/java");
        }catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * -添加将要创建的实体类的信息，会根据类名生成数据库的表，属性名生成数据库的字段
     * -如果建多张表，可以创建多个Entity对象
     * @param schema
     */
    private static void addNote(Schema schema) {
        // 指定需要生成实体类的类名，表名根据类名自动命名
        Entity entity = schema.addEntity("WisdomEntity");

        // 指定自增长主键
        entity.addIdProperty().autoincrement().primaryKey();
        // 添加类的属性，根据属性生成数据库表中的字段
        entity.addStringProperty("english");
        entity.addStringProperty("china");
        // 指定需要生成实体类的类名，表名根据类名自动命名

        Entity entity1 = schema.addEntity("CET4Entity");
        entity1.addIdProperty().autoincrement().primaryKey();
        entity1.addStringProperty("word");
        entity1.addStringProperty("english");
        entity1.addStringProperty("china");
        entity1.addStringProperty("sign");

    }
}