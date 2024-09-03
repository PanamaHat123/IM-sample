"use strict";
Object.defineProperty(exports, "__esModule", { value: true });
const core_1 = require("@nestjs/core");
const app_module_1 = require("./app.module");
const CustomLogger_1 = require("./common/config/CustomLogger");
async function bootstrap() {
    const app = await core_1.NestFactory.create(app_module_1.AppModule, {
        logger: new CustomLogger_1.CustomLogger()
    });
    app.useStaticAssets('public');
    await app.listen(3000);
}
bootstrap();
//# sourceMappingURL=main.js.map