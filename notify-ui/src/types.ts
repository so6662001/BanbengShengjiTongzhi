export type AnnouncementType = 'PRE_NOTICE' | 'UPDATE'
export type ChangeCategory = 'ADD' | 'OPTIMIZE' | 'FIX'

export interface ChangeItem {
  category: ChangeCategory
  categoryDesc?: string
  title: string
  content?: string
}

export interface UpdateData {
  announcementId?: number
  type: AnnouncementType
  title: string
  /** 版本说明 */
  description?: string
  versionNo?: string
  releaseTime?: string
  items: ChangeItem[]
  jumpUrl?: string
}

export interface NotifierOptions {
  /** 客户端身份值（License/租户/设备标识） */
  clientId: string
  productCode: string
  version?: string
  /** 后端 client-api 基地址，如 http://host:8082 */
  apiBase: string
  token?: string
  /** 可选签名头 */
  timestamp?: string
  sign?: string
}
