package com.app.utils

import io.reactivex.Observable
import io.reactivex.subjects.PublishSubject

/*
    Todo Publish events
    RxBus.publish(MessageEvent(1, "Hello, World"))
    RxBus.publish("Testing")
    RxBus.publish(bundle)

    Todo Subscribe for Any events only
     disposable = RxBus.listen(String::class.java).subscribe({
        println("Im a String event $it")
    })

    Todo Unsubscribe
    disposable.dispose()
*/

object RxBus {
    private val publisher = PublishSubject.create<Any>()

    /** @param event Can pass any objects Bundle, String, etc.
     */
    fun publish(event: Any) {
        publisher.onNext(event)
    }

    //Using "ofType" will filter only those events that matches with class type we passed
    /**
     * @param eventType Any class "String.class, Int.class, Double.class or CustomModel.class"
     *
     * for ex:
     *
     * disposable = RxBus.listen(Bundle::class.java).subscribe{ }
     *
     * disposable = RxBus.listen(String::class.java).subscribe{ }
     *
     * disposable = RxBus.listen(Int::class.java).subscribe{ }
     *
     * disposable = RxBus.listen(Double::class.java).subscribe{ }
     */
    fun <T> listen(eventType: Class<T>): Observable<T> = publisher.ofType(eventType)
}