
import {App, AppGroup, User} from "@/services/types.ts";
import { z } from "zod";

/** -------------------- BASIC TYPES -------------------- */
export const SSOPolicyValueSchema = z.enum(["Full", "Partial", "Same Domain", "Disabled"]);
export const MFAPolicyValueSchema = z.enum(["Email", "Phone", "Disabled"]);

/** -------------------- User -------------------- */
export const UserSchema = z.object({
    id: z.number(),
    email: z.string().email(),
    firstName: z.string(),
    lastName: z.string(),
    phone: z.string().optional(),
    lastLoginAt: z.date().optional(),
    mfaEnabled: z.boolean(),
    emailVerified: z.boolean(),
});

/** -------------------- App -------------------- */
export const AppSchema = z.object({
    id: z.number(),
    name: z.string(),
    redirectUris: z.array(z.string()),
    clientId: z.string(),
    clientSecret: z.string(),
    clientSecretExpiresAt: z.string(),
    createdAt: z.string(),
    shortDescription: z.string(),
    scopes: z.array(z.string()),
    responseTypes: z.tuple([z.string()]),
    grantTypes: z.tuple([z.string()]),
    tokenEndpointAuthMethod: z.string(),
    logoUri: z.string(),
    appUrl: z.string(),
    group: z.number(),
    dusterCallbackUri: z.string(),
});

/** -------------------- AppGroup -------------------- */
export const AppGroupSchema = z.object({
    id: z.number(),
    name: z.string(),
    createdAt: z.string(),
    isDefault: z.boolean(),
    ssoPolicy: SSOPolicyValueSchema,
    mfaPolicy: MFAPolicyValueSchema,
});

/** -------------------- CreateAppGroupDTO -------------------- */
export const CreateAppGroupDTOSchema = z.object({
    name: z.string(),
    isDefault: z.boolean(),
    ssoPolicy: SSOPolicyValueSchema,
    mfaPolicy: MFAPolicyValueSchema,
});

/** -------------------- DusterApp -------------------- */
export const DusterAppSchema = z.object({
    id: z.number(),
    clientId: z.string(),
    clientSecret: z.string(),
    tokenFetchMode: z.string(),
    userId: z.number(),
    createdAt: z.string(),
});

/** -------------------- JwkKey -------------------- */
export const JwkKeySchema = z.object({
    kty: z.string(),
    e: z.string(),
    use: z.string(),
    kid: z.string(),
    alg: z.string(),
    iat: z.number().optional(),
    n: z.string(),
});

/** -------------------- JwksResponse -------------------- */
export const JwksResponseSchema = z.object({
    keys: z.array(JwkKeySchema),
});

/** -------------------- UserInfoResponse -------------------- */

export const UserInfoResponseSchema = z.object({
    user: z.unknown(),
    apps: z.array(z.unknown()),
    groups: z.array(z.unknown()),
    redirectUri: z.string().optional().nullable(),
    signature: z.string().optional().nullable(),
});

/** -------------------- LoginResponse -------------------- */
export const LoginResponseSchema = z.object({
    status: z.enum(["SUCCESS", "MFA_REQUIRED", "FAILURE"]),
    time: z.date(),
});

/** -------------------- DashboardStatePropsType -------------------- */
export const DashboardStatePropsTypeSchema = z.object({
    apps: z.array(AppSchema),
    selectedGroup: AppGroupSchema,
    selectedGroupEditing: AppGroupSchema,
    isEditingGroup: z.boolean(),
});

/** -------------------- DashboardHandlersPropType -------------------- */
export const DashboardHandlersPropTypeSchema = z.object({
    handleGroupUpdate: z.function().args(z.string(), z.union([z.string(), z.boolean()])).returns(z.void()),
    handleAppClick: z.function().args(z.number()).returns(z.void()),
    handleGroupSave: z.function().returns(z.promise(z.void())),
    toggleEditGroup: z.function().returns(z.void()),
    handleDeleteGroup: z.function().returns(z.promise(z.void())),
    handleGroupCancel: z.function().returns(z.void()),
});

/** -------------------- ProfileTabProps -------------------- */
export const ProfileTabPropsSchema = z.object({
    active: z.boolean().optional(),
    user: UserSchema,
});

/** -------------------- TotpModalProps -------------------- */
export const TotpModalPropsSchema = z.object({
    dialogOpen: z.boolean(),
    onOpenChange: z.function().args(z.boolean()).returns(z.void()),
    user: UserSchema,
    onSubmit: z.function().args(z.string()).returns(z.promise(z.any())), // AxiosResponse can be any
    onSuccess: z.function().returns(z.void()).optional(),
});
