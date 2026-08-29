import { createContext } from 'react'
import type { DusterClient } from '@authoss/duster-core'

export const DusterContext = createContext<DusterClient | null>(null)
DusterContext.displayName = 'DusterContext'
