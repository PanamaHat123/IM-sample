import { Injectable } from '@nestjs/common';
import { Role } from '../model/entity/Role.entity';
import { RoleDao } from '../dao/role.dao';

@Injectable()
export class RoleService {

 
    
  constructor(
    private roleDao: RoleDao,

  ) { }

  async getRole(): Promise<Role> {
    return this.roleDao.queryRole();
  }

}
