import {api} from "@/services/netconfig.ts";
import {JwkKey, JwksResponse} from "@/services/types.ts";
import {importJWK, jwtVerify} from 'jose';

const getJWKPubKey = async (): Promise<JwkKey> => {
    const resp = await api.get<JwksResponse>("/.well-known/jwks.json");
    const data: JwksResponse = resp.data;
    const pubkey = data.keys.find(k => k.kid === 'authos-jwt-sign');

    if (!pubkey) {
        throw new Error("Public key with kid 'authos-jwt-sign' not found in JWKS.");
    }

    return pubkey;
};

export const validateResponse = async (token: string): Promise<boolean> => {
    const jwk = await getJWKPubKey();
    const pubkey = await importJWK(jwk, 'RS256')
    console.log("token",token)
    try {
        await jwtVerify(token, pubkey, {
            issuer: import.meta.env.API_BASE_URL,
            requiredClaims: ["exp", "sub"]
        })
        console.log("verified")
        return true;
    } catch (err) {
        console.error("cant validate token", err)
        return false;

    }
}
