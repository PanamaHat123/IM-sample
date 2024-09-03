import { Role } from '../model/entity/Role.entity';
import { RoleDao } from '../dao/role.dao';
export declare class RoleService {
    private roleDao;
    constructor(roleDao: RoleDao);
    getRole(): Promise<Role>;
}
