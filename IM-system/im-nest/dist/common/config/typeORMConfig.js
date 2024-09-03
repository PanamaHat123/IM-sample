"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const typeorm_1 = require("@nestjs/typeorm");
const path_1 = require("path");
console.log("__dirname", __dirname);
const entitiesPaths = [(0, path_1.join)(__dirname, '..', "..", '**', '*.entity.{ts,js}')];
console.log("entitiesPaths", entitiesPaths);
exports.default = typeorm_1.TypeOrmModule.forRoot({
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
//# sourceMappingURL=typeORMConfig.js.map