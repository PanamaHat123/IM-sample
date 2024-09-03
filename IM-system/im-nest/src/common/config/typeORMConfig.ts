import { TypeOrmModule } from "@nestjs/typeorm";
import { join } from "path";
import { Role } from "src/role/model/entity/Role.entity";
import { User } from "src/user/entity/User.entity";

console.log("__dirname",__dirname);
const entitiesPaths = [join(__dirname, '..', "..",'**', '*.entity.{ts,js}')]
console.log("entitiesPaths",entitiesPaths);
export default TypeOrmModule.forRoot({
    type: 'mysql',
    host: 'localhost',
    port: 3306,
    username: 'root',
    password: 'root',
    database: 'test',
    synchronize: true,
    logging: true,
    entities: entitiesPaths,
    connectorPackage: 'mysql2'
  });