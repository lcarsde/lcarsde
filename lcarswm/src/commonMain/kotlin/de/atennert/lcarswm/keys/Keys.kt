package de.atennert.lcarswm.keys

import de.atennert.lcarswm.Environment

expect val keyShiftL: Int
expect val keyShiftR: Int
expect val keyControlL: Int
expect val keyControlR: Int
expect val keySuperL: Int
expect val keySuperR: Int
expect val keyHyperL: Int
expect val keyHyperR: Int
expect val keyMetaL: Int
expect val keyMetaR: Int
expect val keyAltL: Int
expect val keyAltR: Int

expect fun keysymToKeycode(env: Environment, keySym: Int): UInt
