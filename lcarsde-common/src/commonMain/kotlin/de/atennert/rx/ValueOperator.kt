package de.atennert.rx

class ValueOperator<X, Y>(val handleValue: (value: X, next: (Y) -> Unit) -> Unit) : Operator<X, Y> {
    override fun call(source: Observable<X>): Observable<Y> {
        return Observable { subscriber ->
            source.subscribe(object : Observer<X> {
                override fun next(value: X) {
                    handleValue(value, subscriber::next)
                }

                override fun error(error: Throwable) {
                    subscriber.error(error)
                    subscriber.unsubscribe()
                }

                override fun complete() {
                    subscriber.complete()
                    subscriber.unsubscribe()
                }
            })
        }
    }
}
