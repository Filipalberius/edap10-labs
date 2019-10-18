#include <pthread.h>
#include <stdbool.h>
#include <stdio.h>
#include <stdlib.h>
#include <string.h>

#include "fail.h"
#include "list.h"
#include "msg_store.h"

struct msg_store {
  // A list of topics. Each element points to another list,
  // holding the messages in that topic.
  struct list *topics;
  pthread_mutex_t mutex;
  pthread_cond_t cond;
};

// one list item, representing a message including metadata
struct message {
  char *username;
  char *text;
};

// ============================================================================

// local helper function: returns true if more topics available to the client
// NOTE: this is only called locally in this file, not from the outside
static bool
any_topic_available(struct msg_store *store,
                    struct client_state *client)
{
  int last_topic_available = list_size(store->topics) - 1;
  return (client->last_topic_published < last_topic_available);
}

// ----------------------------------------------------------------------------

// local helper function: returns true if more messages available to the client
// NOTE: this is only called locally in this file, not from the outside
static bool
any_message_available(struct msg_store *store,
                      struct client_state *client)
{
  int topic_id = client->current_topic_id;
  switch (topic_id) {
    case TOPIC_STATE_NO_TOPIC:
      return false;
    case TOPIC_STATE_LOGOUT_REQUESTED:
    case TOPIC_STATE_DISCONNECTED:
      // return true in this case, to break out of the loop
      // in msg_store_await_message_or_topic below
      return true;
    default:
      if (topic_id < list_size(store->topics)) {
        struct list *topic = list_get(store->topics, topic_id);
        int last_message_available = list_size(topic) - 1;
        return (client->last_message_published < last_message_available);
      } else {
        return false;
      }
  }
}

// ============================================================================

struct msg_store *
msg_store_create()
{

  struct msg_store *store = malloc(sizeof(struct msg_store));

  pthread_mutex_init(&store->mutex, NULL);
  pthread_cond_init(&store->cond, NULL);
  store->topics = list_create();

  return store;
}

// ----------------------------------------------------------------------------

int
msg_store_add_topic(struct msg_store *store,
                    char *username,
                    char *text)
{
  pthread_mutex_lock(&store->mutex);

  struct message *m = malloc(sizeof(struct message));
  m->username = strdup(username);
  m->text = strdup(text);

  struct list *topic = list_create();
  list_add(topic, m);
  list_add(store->topics, topic);
  int topic_id = list_size(store->topics) - 1;

  pthread_mutex_unlock(&store->mutex);
  pthread_cond_broadcast(&store->cond);

  return topic_id;
}

// ----------------------------------------------------------------------------

void
msg_store_add_message(struct msg_store *store,
                      struct client_state *client,
                      char *username,
                      char *text)
{
  pthread_mutex_lock(&store->mutex);

  struct message *m = malloc(sizeof(struct message));
  m->username = strdup(username);
  m->text = strdup(text);
  if (client->current_topic_id >= list_size(store->topics)) {
    printf("[%p] attempting to add message to topic %d (list size=%d), ignored\n",
           client, client->current_topic_id, list_size(store->topics));
  }
  struct list *topic = list_get(store->topics, client->current_topic_id);
  list_add(topic, m);

  pthread_mutex_unlock(&store->mutex);
  pthread_cond_broadcast(&store->cond);
}

// ----------------------------------------------------------------------------

bool
msg_store_poll_topic(struct msg_store *store,
                     struct client_state *client,
                     int *topic_id,
                     int *nbr_messages,
                     char **username,
                     char **text)
{
  pthread_mutex_lock(&store->mutex);

  bool available = any_topic_available(store, client);
  if (available) {
    client->last_topic_published++;
    *topic_id = client->last_topic_published;
    struct list *topic = list_get(store->topics, client->last_topic_published);
    *nbr_messages = list_size(topic);

    // the first message in the topic becomes the heading for the topic
    struct message *m = list_get(topic, 0);
    *username = m->username;
    *text = m->text;
  }

  pthread_mutex_unlock(&store->mutex);
  pthread_cond_broadcast(&store->cond);

  return available;
}

// ----------------------------------------------------------------------------

bool
msg_store_poll_message(struct msg_store *store,
                       struct client_state *client,
                       int *topic_id,
                       int *message_id,
                       char **username,
                       char **text)
{
  pthread_mutex_lock(&store->mutex);

  bool available = any_message_available(store, client)
                   && (client->current_topic_id >= 0)
                   && (client->current_topic_id < list_size(store->topics));
  if (available) {
    client->last_message_published++;
    struct list *topic = list_get(store->topics, client->current_topic_id);
    if (client->last_message_published >= list_size(topic)) {
      available = false;
    } else {
      struct message *m = list_get(topic, client->last_message_published);
      *topic_id = client->current_topic_id;
      *message_id = client->last_message_published;
      *username = m->username;
      *text = m->text;
    }
  }

  pthread_mutex_unlock(&store->mutex);
  pthread_cond_broadcast(&store->cond);

  return available;
}

// ----------------------------------------------------------------------------

void
msg_store_init_client(struct msg_store *store,
                      struct client_state *client)
{
  pthread_mutex_lock(&store->mutex);
  client->current_topic_id = TOPIC_STATE_NO_TOPIC;
  client->last_topic_published = -1;     // force update
  pthread_mutex_unlock(&store->mutex);
}

// ----------------------------------------------------------------------------

void
msg_store_select_topic(struct msg_store *store,
                       struct client_state *client,
                       int topic_id)
{
  pthread_mutex_lock(&store->mutex);
  client->current_topic_id = topic_id;
  client->last_message_published = -1;   // force update
  pthread_mutex_unlock(&store->mutex);
  pthread_cond_broadcast(&store->cond);
}

// ----------------------------------------------------------------------------

int
msg_store_await_message_or_topic(struct msg_store *store,
                                 struct client_state *client)
{
  pthread_mutex_lock(&store->mutex);

  while (! any_topic_available(store, client)
      && ! any_message_available(store, client))
  {
    pthread_cond_wait(&store->cond, &store->mutex);
  }

  int reading_state = client->current_topic_id;

  pthread_mutex_unlock(&store->mutex);

  return reading_state;
}
