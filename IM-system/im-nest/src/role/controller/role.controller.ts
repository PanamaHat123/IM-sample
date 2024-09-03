import { Controller, Get,Logger } from '@nestjs/common';
import { Role } from '../model/entity/Role.entity';
import { RoleService } from '../service/role.service';

@Controller('role')
export class RoleController {
  private readonly logger = new Logger(RoleController.name);
  constructor(private roleService:RoleService) { }

  @Get("/getOne")
  getUser(): Promise<Role> {
    this.logger.log("getUser 进来了");
    const rolePromise = this.roleService.getRole()
    this.logger.log("getUser 出去了",rolePromise);
    return rolePromise;
  }

}
