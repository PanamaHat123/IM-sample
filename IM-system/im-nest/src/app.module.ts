import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { UserModule } from './user/user.module';
import { UserController } from './user/controller/user.controller';
import { RoleModule } from './role/role.module';
import TypeORMDefault from "src/common/config/typeORMConfig"
import { CustomLogger } from './common/config/CustomLogger';

@Module({
  imports: [
    UserModule,
    RoleModule,
    TypeORMDefault
  ],
  controllers: [],
  providers: [
    {
      provide: 'Logger',
      useClass: CustomLogger,
    }
  ],
})
export class AppModule {

}
