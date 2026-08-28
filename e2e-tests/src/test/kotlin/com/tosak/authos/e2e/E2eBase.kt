package com.tosak.authos.e2e

import com.tosak.authos.e2e.support.E2eFixture
import com.tosak.authos.e2e.support.Http
import com.tosak.authos.e2e.support.OAuthFlow
import com.tosak.authos.e2e.support.StackExtension
import org.junit.jupiter.api.extension.ExtendWith

@ExtendWith(StackExtension::class)
abstract class E2eBase {
    protected val fx: E2eFixture get() = StackExtension.fixture
    protected val flow: OAuthFlow get() = OAuthFlow(fx)
    protected fun http() = Http()
}
