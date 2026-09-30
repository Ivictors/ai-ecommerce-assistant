export type ChatResponseType =
  | 'TEXT'
  | 'PRODUCT_INFORMATION'
  | 'ORDER_STATUS'
  | 'POLICY_INFORMATION'
  | 'FALLBACK';

export interface ProductInformationData {
  id: number;
  name: string;
  description: string;
  price: number;
  stock: number;
  active: boolean;
}

export interface OrderStatusData {
  id: number;
  status: string;
  total: number;
  createdAt: string;
}

export interface PolicyInformationData {
  content: string;
}

export interface ChatResponse {
  type: ChatResponseType;
  schemaVersion: string;
  message: string;
  data: ProductInformationData | OrderStatusData | PolicyInformationData | null;
}
