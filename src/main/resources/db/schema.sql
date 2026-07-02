CREATE DATABASE IF NOT EXISTS frankenstein DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci COMMENT 'Frankenstein application database';
USE frankenstein;

-- ---------------------------------------------------------------------------
-- Admin accounts (back-office)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_admin (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    username        VARCHAR(64)  NOT NULL                COMMENT 'Login username, unique',
    password        VARCHAR(128) NOT NULL                COMMENT 'BCrypt password hash',
    nickname        VARCHAR(64)  DEFAULT NULL           COMMENT 'Display name',
    phone           VARCHAR(20)  DEFAULT NULL           COMMENT 'Mobile phone number',
    email           VARCHAR(128) DEFAULT NULL           COMMENT 'Email address',
    status          TINYINT      NOT NULL DEFAULT 1     COMMENT 'Account status: 0=disabled, 1=active',
    last_login_time DATETIME     DEFAULT NULL           COMMENT 'Last successful login time',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record last update time',
    deleted         TINYINT      NOT NULL DEFAULT 0     COMMENT 'Logical delete flag: 0=not deleted, 1=deleted',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_username (username)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Back-office administrator accounts';

-- ---------------------------------------------------------------------------
-- Member accounts (mall / user client)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_member (
    id              BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    username        VARCHAR(64)  NOT NULL                COMMENT 'Login username, unique',
    password        VARCHAR(128) NOT NULL                COMMENT 'BCrypt password hash',
    nickname        VARCHAR(64)  DEFAULT NULL           COMMENT 'Display name',
    phone           VARCHAR(20)  DEFAULT NULL           COMMENT 'Mobile phone number, unique when set',
    email           VARCHAR(128) DEFAULT NULL           COMMENT 'Email address, unique when set',
    vip_level       TINYINT      NOT NULL DEFAULT 0     COMMENT 'VIP level: 0=normal, 1=vip (affects max login devices)',
    status          TINYINT      NOT NULL DEFAULT 1     COMMENT 'Account status: 0=disabled, 1=active',
    register_source VARCHAR(32)  DEFAULT 'web'          COMMENT 'Registration channel, e.g. web, app, h5',
    last_login_time DATETIME     DEFAULT NULL           COMMENT 'Last successful login time',
    create_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
    update_time     DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record last update time',
    deleted         TINYINT      NOT NULL DEFAULT 0     COMMENT 'Logical delete flag: 0=not deleted, 1=deleted',
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_username (username),
    UNIQUE KEY uk_member_phone (phone),
    UNIQUE KEY uk_member_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Mall member / end-user accounts';

-- ---------------------------------------------------------------------------
-- Roles (scoped to ADMIN or MEMBER)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_role (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    role_code   VARCHAR(64)  NOT NULL                COMMENT 'Role code, unique, e.g. ADMIN_SUPER',
    role_name   VARCHAR(64)  NOT NULL                COMMENT 'Human-readable role name',
    role_scope  VARCHAR(16)  NOT NULL                COMMENT 'Applicable account scope: ADMIN or MEMBER',
    status      TINYINT      NOT NULL DEFAULT 1     COMMENT 'Role status: 0=disabled, 1=active',
    remark      VARCHAR(255) DEFAULT NULL           COMMENT 'Optional description',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record last update time',
    deleted     TINYINT      NOT NULL DEFAULT 0     COMMENT 'Logical delete flag: 0=not deleted, 1=deleted',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_code (role_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RBAC roles for admin and member accounts';

-- ---------------------------------------------------------------------------
-- Permissions (menu / button / API)
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_permission (
    id          BIGINT       NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    perm_code   VARCHAR(128) NOT NULL                COMMENT 'Permission code, format scope:module:action',
    perm_name   VARCHAR(64)  NOT NULL                COMMENT 'Human-readable permission name',
    perm_type   VARCHAR(16)  NOT NULL DEFAULT 'API'  COMMENT 'Permission type: MENU, BUTTON, or API',
    parent_id   BIGINT       DEFAULT 0               COMMENT 'Parent permission ID for tree structure, 0=root',
    path        VARCHAR(255) DEFAULT NULL           COMMENT 'API path or frontend route path',
    method      VARCHAR(16)  DEFAULT NULL           COMMENT 'HTTP method for API permissions, e.g. GET, POST',
    sort        INT          DEFAULT 0               COMMENT 'Display sort order, ascending',
    status      TINYINT      NOT NULL DEFAULT 1     COMMENT 'Permission status: 0=disabled, 1=active',
    create_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT 'Record creation time',
    update_time DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT 'Record last update time',
    deleted     TINYINT      NOT NULL DEFAULT 0     COMMENT 'Logical delete flag: 0=not deleted, 1=deleted',
    PRIMARY KEY (id),
    UNIQUE KEY uk_perm_code (perm_code)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='RBAC permissions for menus, buttons, and APIs';

-- ---------------------------------------------------------------------------
-- Role-permission mapping
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_role_permission (
    id            BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    role_id       BIGINT NOT NULL                COMMENT 'Foreign key to sys_role.id',
    permission_id BIGINT NOT NULL                COMMENT 'Foreign key to sys_permission.id',
    PRIMARY KEY (id),
    UNIQUE KEY uk_role_perm (role_id, permission_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Many-to-many mapping between roles and permissions';

-- ---------------------------------------------------------------------------
-- Admin-role mapping
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_admin_role (
    id       BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    admin_id BIGINT NOT NULL                COMMENT 'Foreign key to sys_admin.id',
    role_id  BIGINT NOT NULL                COMMENT 'Foreign key to sys_role.id (role_scope must be ADMIN)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_admin_role (admin_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Many-to-many mapping between admin accounts and roles';

-- ---------------------------------------------------------------------------
-- Member-role mapping
-- ---------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS sys_member_role (
    id        BIGINT NOT NULL AUTO_INCREMENT COMMENT 'Primary key',
    member_id BIGINT NOT NULL                COMMENT 'Foreign key to sys_member.id',
    role_id   BIGINT NOT NULL                COMMENT 'Foreign key to sys_role.id (role_scope must be MEMBER)',
    PRIMARY KEY (id),
    UNIQUE KEY uk_member_role (member_id, role_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='Many-to-many mapping between member accounts and roles';

-- ---------------------------------------------------------------------------
-- Seed data
-- ---------------------------------------------------------------------------
INSERT IGNORE INTO sys_role (id, role_code, role_name, role_scope, remark) VALUES
(1, 'ADMIN_SUPER', 'Super Admin', 'ADMIN', 'Full admin access'),
(2, 'MEMBER_NORMAL', 'Normal Member', 'MEMBER', 'Default member role');

INSERT IGNORE INTO sys_permission (id, perm_code, perm_name, perm_type, path, method) VALUES
(1, 'admin:auth:info', 'Admin auth info', 'API', '/admin/api_v1/auth/info', 'GET'),
(2, 'admin:member:list', 'List members', 'API', '/admin/api_v1/member/list', 'GET'),
(3, 'member:auth:info', 'Member auth info', 'API', '/user/api_v1/auth/info', 'GET'),
(4, 'member:order:view', 'View orders', 'API', '/user/api_v1/order/list', 'GET');

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 1, id FROM sys_permission WHERE perm_code LIKE 'admin:%';

INSERT IGNORE INTO sys_role_permission (role_id, permission_id)
SELECT 2, id FROM sys_permission WHERE perm_code LIKE 'member:%';

-- Default admin account is created on first startup by DataInitializer (admin / Admin@123)
