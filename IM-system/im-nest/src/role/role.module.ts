import { Module } from '@nestjs/common';
import { RoleService } from './service/role.service';
import { RoleController } from './controller/role.controller';
import TypeORMDefault from 'src/common/config/typeORMConfig';
import { RoleDao } from './dao/role.dao';

@Module({
  providers: [RoleService,RoleDao],
  controllers: [RoleController]
})
export class RoleModule {}
