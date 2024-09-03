import { Role } from '../model/entity/Role.entity';
import { RoleService } from '../service/role.service';
export declare class RoleController {
    private roleService;
    private readonly logger;
    constructor(roleService: RoleService);
    getUser(): Promise<Role>;
}
